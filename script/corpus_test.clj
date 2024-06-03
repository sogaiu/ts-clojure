(ns corpus-test
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [utils :as u]
            [conf :as cnf]))

(defn -main
  [& _args]
  (u/exit-unless-grammar-dir-exists)
  (try
    (let [p (proc/shell {:dir cnf/grammar-dir}
                        (str cnf/ts-bin-path " test"))
          exit-code (:exit @p)]
      (u/exit-unless-error-code-is exit-code #{0} "tree-sitter test"))
    (catch Exception e
      (u/report-exception-and-exit e))))

