(ns ensure-tree-sitter-grammar
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [babashka.tasks :as t]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when-not (fs/exists? (cnf/grammar :dir))
    (try
      (t/shell (str "git clone " (cnf/grammar :repo-url)))
      (catch Exception e
        (println "Problem cloning tree-sitter grammar:" (.getMessage e))
        (System/exit 1)))
    ;; check out commit
    (try
      (println "Checking out commit:" (cnf/grammar :ref))
      (let [p (proc/process {:dir (cnf/grammar :dir)}
                            "git" "checkout" (cnf/grammar :ref))
              exit-code (:exit @p)]
        (when-not (zero? exit-code)
          (println "git checkout exited non-zero:" exit-code)
          (System/exit 1)))
      (catch Exception e
        (println "Problem checking out commit:" (.getMessage e))
        (System/exit 1)))))

