(ns clean
  (:require [babashka.fs :as fs]
            [clojure.java.io :as cji]
            [conf :as cnf])
  (:import [java.io File]))

(defn -main
  [& _args]
  ;; remove generated / compiled tree-sitter-clojure/src content
  (doseq [fs-path (concat cnf/generated-source-paths
                          ;; XXX: hard-wired parser.o
                          [(str cnf/tsclj-src-dir "/parser.o")
                           (str cnf/tsclj-src-dir "/" cnf/tsclj-lib-name)])]
    (when (fs/exists? fs-path)
      (cond
        (fs/directory? fs-path)
        (fs/delete-tree fs-path)
        ;;
        (.isFile (cji/file fs-path))
        (fs/delete fs-path)
        ;;
        :else
        (do
          (println "Unexpected fs item found:" fs-path)
          (System/exit 1))))))


