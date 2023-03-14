(ns uninstall-dynamic-library
  (:require [babashka.fs :as fs]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when (fs/exists? (cnf/grammar :lib-path))
    (fs/delete (cnf/grammar :lib-path))))


