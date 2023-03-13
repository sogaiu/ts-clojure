(ns uninstall-dynamic-library
  (:require [babashka.fs :as fs]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when (fs/exists? cnf/tsclj-dynamic-library-path)
    (fs/delete cnf/tsclj-dynamic-library-path)))


