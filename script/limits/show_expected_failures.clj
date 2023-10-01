(ns show-expected-failures
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when (fs/exists? cnf/fails-root)
    (fs/walk-file-tree
     cnf/fails-root
     {:visit-file
      (fn [path _]
        (println "Expecting ERROR for:" (fs/file-name path))
        ;; https://github.com/babashka/process#shell
        (let [exit-code
              (-> (proc/shell {:continue true
                               :extra-env {"TREE_SITTER_DIR" cnf/ts-conf-dir
                                           "TREE_SITTER_LIBDIR" cnf/ts-lib-dir}}
                              (str cnf/ts-bin-path " parse --quiet " path))
                  :exit)]
          (if (= exit-code 1)
            (println "Exit-code was 1 as expected")
            (println "Unexpected exit-code:" exit-code)))
        (println)
        :continue)})))
