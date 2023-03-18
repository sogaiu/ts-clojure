(ns ensure-emsdk
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [babashka.tasks :as t]
            [conf :as cnf]))

(defn -main
  [& _args]
  ;; clone emsdk repository if necessary
  (when-not (fs/exists? "emsdk")
    (try
      (t/shell (str "git clone " cnf/emsdk-repo-url))
      (catch Exception e
        (println "Problem cloning emsdk:" (.getMessage e))
        (System/exit 1)))
    ;; install emsdk
    (try
      (println "Installing emsdk...")
      (let [p (proc/process {:dir "emsdk"}
                            "./emsdk" "install" cnf/emsdk-version)
              exit-code (:exit @p)]
        (when-not (zero? exit-code)
          (println "emsdk exited non-zero:" exit-code)
          (System/exit 1)))
      (catch Exception e
        (println "Problem installing emsdk:" (.getMessage e))
        (System/exit 1)))
    ;; activate emsdk
    (try
      (println "Activating emsdk...")
      (let [p (proc/process {:dir "emsdk"}
                            "./emsdk" "activate" cnf/emsdk-version)
              exit-code (:exit @p)]
        (when-not (zero? exit-code)
          (println "emsdk exited non-zero:" exit-code)
          (System/exit 1)))
      (catch Exception e
        (println "Problem activating emsdk:" (.getMessage e))
        (System/exit 1)))))
