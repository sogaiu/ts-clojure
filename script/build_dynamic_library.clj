(ns build-dynamic-library
  (:require [babashka.process :as proc]
            [conf :as cnf]))

(defn -main
  [& _args]
  ;; compile
  (let [p (proc/process {:dir cnf/tsclj-src-dir
                         :out :string
                         :err :string}
                        (str cnf/c-compiler
                             " -fPIC"
                             " -c"
                             " -I."
                             " " cnf/parser-c-name))
        exit-code (:exit @p)]
    (when (not (zero? exit-code))
      (println "Problem compiling parser.c:" exit-code)
      (println (:out @p))
      (println (:err @p))
      (System/exit 1))
    ;; link
    (let [p (proc/process {:dir cnf/tsclj-src-dir
                           :out :string
                           :err :string}
                          (str cnf/c-compiler
                               " -fPIC"
                               " -shared parser.o" ;; XXX: hard-wired
                               " -o " cnf/tsclj-lib-name))
          exit-code (:exit @p)]
      (when (not (zero? exit-code))
        (println "Problem linking:" exit-code)
        (println (:out @p))
        (println (:err @p))
        (System/exit 1)))))

