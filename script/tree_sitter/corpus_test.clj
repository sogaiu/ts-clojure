(ns corpus-test
  (:require [babashka.process :as proc]
            [conf :as cnf]))

(defn -main
  [& _args]
  (let [p (proc/process {:dir (cnf/grammar :dir)
                         :out :string}
                        (str cnf/ts-bin-path " test"))
        exit-code (:exit @p)]
    (when (not (zero? exit-code))
      (println "Problem running corpus test:" exit-code)
      (System/exit 1))
    (print (:out @p))))

