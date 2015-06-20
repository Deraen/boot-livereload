(set-env!
  :resource-paths #{"src"}
  :dependencies   '[[org.clojure/clojure "1.6.0"       :scope "provided"]
                    [boot/core           "2.1.2"       :scope "provided"]
                    [adzerk/bootlaces    "0.1.8"       :scope "test"]
                    [clj-livereload      "0.2.0"       :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.1")

(bootlaces! +version+)

(task-options!
  pom {:project     'deraen/boot-livereload
       :version     +version+
       :description "Provides LiveReload.js compatibly server as Boot task"
       :url         "https://github.com/deraen/boot-livereload"
       :scm         {:url "https://github.com/deraen/boot-livereload"}
       :license     {"MIT" "http://opensource.org/licenses/MIT"}})

(deftask dev
  "Dev process"
  []
  (comp
    (watch)
    (repl :server true)
    (pom)
    (jar)
    (install)))
