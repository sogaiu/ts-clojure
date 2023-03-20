(ns gen-grammar-json
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [conf :as cnf]))

;; https://github.com/sogaiu/tree-sitter-clojure/pull/26#issuecomment-1371776963
(defn -main
  [& _args]
  (if-not (fs/exists? (cnf/grammar :grammar-js))
    (println "grammar.js not found")
    (let [pl (proc/pipeline
              (-> (proc/process
                   ;; XXX: not cross-platform?
                   (str "cat "
                        "./tree-sitter/cli/src/generate/dsl.js"))
                  (proc/process
                   {:dir (cnf/grammar :dir) ;; needs to be here
                    :extra-env {"TREE_SITTER_GRAMMAR_PATH"
                                (cnf/grammar :grammar-js)}
                    :out :string}
                   ;; XXX: could make this some other js runtime
                   "node")))
          np (last pl)
          exit-code (:exit @np)]
      (when-not (zero? exit-code)
        (println "Problem generating grammar.json:" exit-code)
        (System/exit 1))
      ;; XXX: save to file?
      (println (:out @np)))))

