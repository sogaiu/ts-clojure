(ns ensure-tree-sitter-clojure
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [babashka.tasks :as t]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when-not (fs/exists? cnf/tsclj-dir)
    (try
      (t/shell (str "git clone " cnf/tsclj-repo-url))
      (catch Exception e
        (println "Problem cloning tree-sitter-clojure:" (.getMessage e))
        (System/exit 1)))
    ;; check out commit tsclj-ref
    (try
      (println "Checking out commit:" cnf/tsclj-ref)
      (let [p (proc/process {:dir cnf/tsclj-dir}
                            "git" "checkout" cnf/tsclj-ref)
              exit-code (:exit @p)]
        (when-not (zero? exit-code)
          (println "git checkout exited non-zero:" exit-code)
          (System/exit 1)))
      (catch Exception e
        (println "Problem checking out commit:" (.getMessage e))
        (System/exit 1)))))

