(ns install-dynlib
  (:require [babashka.fs :as fs]
            [conf :as cnf]))

(defn -main
  [& _args]
  ;; XXX: shouldn't compute the path here?
  (let [built-path (str (cnf/grammar :src-dir) "/" (cnf/grammar :lib-name))]
    (when (fs/exists? built-path)
      (fs/copy built-path
               (cnf/grammar :lib-path)
               {:replace-existing true}))))

