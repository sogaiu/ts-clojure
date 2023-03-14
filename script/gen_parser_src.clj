(ns gen-parser-src
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [conf :as cnf]))

(defn -main
  [& _args]
  (if-not (fs/exists? (cnf/grammar :grammar-js))
    (println "grammar.js not found")
    (let [p (proc/process {:dir (cnf/grammar :dir)} cnf/ts-generate-cmd)
          exit-code (:exit @p)]
      (when (not (zero? exit-code))
        (println "Problem generating parser source:" exit-code)))))

