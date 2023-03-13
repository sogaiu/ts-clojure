(ns install-dynamic-library
  (:require [babashka.fs :as fs]
            [conf :as cnf]))

(defn -main
  [& _args]
  ;; XXX: shouldn't compute the path here?
  (let [built-path (str cnf/tsclj-src-dir "/" cnf/tsclj-lib-name)]
    (when (fs/exists? built-path)
      (fs/copy built-path
               cnf/tsclj-dynamic-library-path
               {:replace-existing true}))))

