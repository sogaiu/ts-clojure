(ns check-rust-bits
  (:require [babashka.fs :as fs]))

(defn -main
  [& _args]
  (when-not (and (fs/which "rustc")
                 (fs/which "cargo"))
    (println "Did not find both rustc and cargo.")
    (System/exit 1)))
