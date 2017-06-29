(set-env!
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                  [boot/core "2.7.0" :scope "provided"]
                  [cheshire "5.7.0" :scope "test"]
                  [ring "1.5.1" :scope "test"]
                  [http-kit "2.2.0" :scope "test"]
                  [org.webjars.npm/livereload-js "2.2.2" :scope "test"]])

(def +version+ "0.2.1")

(task-options!
  pom {:project     'deraen/boot-livereload
       :version     +version+
       :description "Provides LiveReload.js compatibly server as Boot task"
       :url         "https://github.com/deraen/boot-livereload"
       :scm         {:url "https://github.com/deraen/boot-livereload"}
       :license     {"MIT" "http://opensource.org/licenses/MIT"}})

(deftask build []
  (comp
    (pom)
    (jar)
    (install)))

(deftask dev
  "Dev process"
  []
  (comp
    (watch)
    (repl :server true)
    (build)))

(deftask deploy []
  (comp
    (build)
    (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))
