(ns deraen.boot-livereload
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [boot.pod        :as pod]
            [boot.core       :as core]
            [boot.util       :as util]))

(def ^:private deps
  '[[clj-livereload "0.1.1"]])

(defn asset-pathify [url asset-path]
  (if asset-path
    (if (.startsWith url asset-path)
      (string/replace url (re-pattern (str "^" asset-path "/")) ""))
    url))

(core/deftask livereload
  "Start LiveReload.js server"
  [a asset-path PATH   str   "Set asset-path. If you are only serving files from
                         certain prefix inside the fileset, you can use this to
                         remove that prefix from urls before they are sent to the
                         browser."
   p port       PORT   int   "Non-default port not recommended. Set port for LiveReload server."
   s silent            bool  "Silence all output."
   f filter     FILTER regex "Test if urls match this regex before sending
                         messages to the browser. Useful to filter out
                         unnecessary changes which might cause full page reload.
                         E.g. .css.map, .less.
                         Example: #\"\\.(css|html|js)\""]
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
