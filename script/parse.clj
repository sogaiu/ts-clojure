(ns parse
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [clojure.string :as cs]
            [conf :as cnf]))

(defn -main
  [& _args]
  ;; XXX: is this use of *command-line-args* ok?
  (let [args-as-string
        (->> *command-line-args*
             (map fs/absolutize)
             (cs/join " "))
        p (proc/process {:dir (cnf/grammar :dir)
                         :out :string}
                        (str cnf/ts-bin-path " parse " args-as-string))
        exit-code (:exit @p)]
    (when (not (zero? exit-code))
      (println "Problem parsing:" exit-code)
      (System/exit 1))
    (print (:out @p))))

