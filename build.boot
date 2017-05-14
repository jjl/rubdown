(set-env!
  :project 'irresponsible/rubdown
  :version "0.1.0"
  :resource-paths #{"src"}
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.9.0-alpha16" :scope "provided"]
                  [irresponsible/spectra "0.1.0"]
                  [com.vladsch.flexmark/flexmark-all "0.19.3"]
                  [org.clojure/clojurescript "1.9.456" :scope "test"]
                  [criterium                 "0.4.4"   :scope "test"]
                  [irresponsible/gadget      "0.2.0"   :scope "test"]
                  [adzerk/boot-test          "1.1.0"   :scope "test"]])

(require '[adzerk.boot-test :as t])

(task-options!
  pom {:project (get-env :project)
       :version (get-env :version)
       :description "Flexible, performant markdown parsing for clojure"
       :url "https://github.com/irresponsible/rubdown"
       :scm {:url "https://github.com/irresponsible/rubdown"}
       :license {"MIT" "https://en.wikipedia.org/MIT_License"}}
  target  {:dir #{"target"}})

(deftask testing []
  (set-env! :source-paths   #(conj % "test")
            :resource-paths #(conj % "test"))
  identity)

(deftask test []
  (comp (testing) (speak) (t/test)))

(deftask autotest []
  (comp (testing) (watch) (test)))

(deftask make-jar []
  (comp (javac) (pom) (jar)))

(deftask travis []
  (comp (testing) (t/test)))
