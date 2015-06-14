(ns deraen.boot-livereload
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [boot.pod        :as pod]
            [boot.core       :as core]
            [boot.util       :as util]))

(def ^:private deps
  '[[clj-livereload "0.1.1"]])

(defn foo [url asset-path]
  (if asset-path
    (if (.startsWith url asset-path)
      (string/replace url (re-pattern (str "^" asset-path "/")) ""))
    url))

(core/deftask livereload
  "Start LiveReload.js server"
  [a asset-path PATH str ""]
  (let [pod (-> (core/get-env)
                (update-in [:dependencies] into deps)
                pod/make-pod
                future)
        prev (atom nil)
        start (delay
                (pod/with-eval-in @pod
                  (require 'clj-livereload.core)
                  ((resolve 'clj-livereload.core/start-server))))]
    (core/with-pre-wrap fileset
      @start
      (let [changes (->> fileset
                         (core/fileset-diff @prev)
                         core/input-files)]
        (doseq [change changes
                :let [url (foo (core/tmp-path change) asset-path)]
                :when url]
          (pod/with-eval-in @pod
            (require 'clj-livereload.core)
            ((resolve 'clj-livereload.core/send-reload-msg) ~url)))
        (reset! prev fileset)))))
