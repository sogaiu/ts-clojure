(ns playground
  (:require [babashka.process :as proc]
            [conf :as cnf]))

(defn -main
  [& _args]
  (println "Starting playground...")
  (flush)
  (let [p (proc/process {:dir (cnf/grammar :dir)}
                        cnf/ts-bin-path "playground")
          exit-code (:exit @p)]
    (when-not (zero? exit-code)
      (println "Problem starting tree-sitter playground:" exit-code))))
