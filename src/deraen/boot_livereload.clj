(ns deraen.boot-livereload
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [boot.pod :as pod]
            [boot.core :as core]
            [boot.util :as util]))

(def ^:private deps
  '[[cheshire "5.7.0"]
    [ring "1.5.1"]
    [http-kit "2.2.0"]
    [org.webjars.npm/livereload-js "2.2.2"]])

(defn asset-pathify [url asset-path]
  (if asset-path
    (if (.startsWith url asset-path)
      (string/replace url (re-pattern (str "^" (string/replace asset-path #"^/" "") "/")) ""))
    url))

(defn snippet [port]
  (str "<script>document.write('<script src=\"http://' + (location.host || 'localhost').split(':')[0] + ':" port "/livereload.js?snipver=1\"></' + 'script>')</script></body>"))

(snippet 1234)

(defn add-snippet [tmp tmp-file port]
  (let [out-file (io/file tmp (core/tmp-path tmp-file))]
    (let [in-file (core/tmp-file tmp-file)]
      (io/make-parents out-file)
      (spit out-file (string/replace (slurp in-file) #"</body>" (snippet port))))))

(core/deftask livereload
  "Start LiveReload.js server.

   If you are serving files from certain prefix inside the fileset, you should
   set the asset-path options. The asset-path is stripped from start of urls
   before reload message is sent to the browser.

   If you want to make sure full reloads don't happen when certain files
   change, you can use filter to set a regex which the urls have to match.
   E.g. .css.map or .less files might cause reloads when LESS is compiled.
   Example: #\"\\.(css|html|js)$\".

   Default port is 35729 and can be used with the LiveReload browser extension
   (https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei?hl=en).
   If the snippet option is enabled, script is added to any HTML files automatically
   and random port is used."
  [a asset-path PATH   str   "Asset-path"
   s snippet           bool  "Add script automatically to HTML files"
   p port       PORT   int   "Port (if snippet enabled, default is random, else default is 35729)"
   f filter     FILTER regex "Filter"]
  (let [pod (-> (core/get-env)
                (update-in [:dependencies] into deps)
                pod/make-pod
                future)
        prev (atom nil)
        debug (>= @util/*verbosity* 2)
        start (delay
                (pod/with-call-in @pod
                  (deraen.boot-livereload.impl/start! {:port ~(or port (if snippet 0 35729))})))
        tmp (core/tmp-dir!)]
    (fn [next-handler]
      (fn [fileset]
        (let [{:keys [port]} @start]

          (doseq [html-file (->> fileset
                                 (core/fileset-diff @prev fileset :hash)
                                 (core/input-files)
                                 (core/by-ext [".html"]))]
            (add-snippet tmp html-file port))

          (let [changes (core/input-files (core/fileset-diff @prev fileset :hash))]
            (doseq [change changes
                    :let [url (asset-pathify (core/tmp-path change) asset-path)]
                    :when (and url (or (not filter) (re-find filter url)))]
              (pod/with-call-in @pod (deraen.boot-livereload.impl/send-reload-msg! ~url)))
            (reset! prev fileset))

          (-> fileset (core/add-resource tmp) core/commit! next-handler))))))
