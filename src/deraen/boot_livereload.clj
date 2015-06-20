(ns deraen.boot-livereload
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [boot.pod        :as pod]
            [boot.core       :as core]
            [boot.util       :as util]))

(def ^:private deps
  '[[clj-livereload "0.2.0"]])

(defn asset-pathify [url asset-path]
  (if asset-path
    (if (.startsWith url asset-path)
      (string/replace url (re-pattern (str "^" (string/replace asset-path #"^/" "") "/")) ""))
    url))

(core/deftask livereload
  "Start LiveReload.js server.

   If you are serving files from certain prefix inside the filset, you should
   set the asset-path options. The asset-path is stripped from start of urls
   before reload message is sent to the browser.

   If you want to make sure full reloads don't happen when certains files
   change you can use filter to set a regex which the urls have to match.
   E.g. .css.map or .less files might cause reloads when LESS is compiled.
   Example: #\"\\.(css|html|js)$\".

   Using non-standard port is not recommended as then the browser plugin can't
   connect to the server and you'll have to manually add the snippet to your
   html. http://feedback.livereload.com/knowledgebase/articles/86180-how-do-i-add-the-script-tag-manually-"
  [a asset-path PATH   str   "asset-path"
   p port       PORT   int   "port"
   s silent            bool  "Silence all output."
   f filter     FILTER regex "filter"]
  (let [pod (-> (core/get-env)
                (update-in [:dependencies] into deps)
                pod/make-pod
                future)
        prev (atom nil)
        debug (>= @util/*verbosity* 2)
        start (delay
                (pod/with-call-in @pod
                  (clj-livereload.server/start! {:port ~port
                                                 :silent? ~silent
                                                 :debug? ~debug})))]
    (core/with-pre-wrap fileset
      @start
      (let [changes (core/input-files (core/fileset-diff @prev fileset :hash))]
        (doseq [change changes
                :let [url (asset-pathify (core/tmp-path change) asset-path)]
                :when (and url (or (not filter) (re-find filter url)))]
          (pod/with-call-in @pod
            (clj-livereload.server/send-reload-msg! ~url)))
        (reset! prev fileset)))))
