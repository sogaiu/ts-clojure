(ns generate-parser
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when-not (fs/exists? (cnf/grammar :dir))
    (println "Directory for" (cnf/grammar :dir) "not found")
    (System/exit 1))
  (println "Generating parser.c")
  (try
    (let [p (proc/shell {:dir (cnf/grammar :dir)}
                        (str cnf/ts-bin-path
                             " generate --abi " cnf/abi " --no-bindings"))
          exit-code (:exit @p)]
      (when-not (#{0} exit-code)
        (println "tree-sitter generate exited with unexpected exit-code:"
                 exit-code)
        (System/exit 1)))
    (catch Exception e
      (println "Exception:" (.getMessage e))
      (System/exit 1))))

