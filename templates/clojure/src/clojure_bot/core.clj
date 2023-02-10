(ns clojure-bot.core
  (:require [clojure-bot.api :as api])
  (:gen-class))

(def game-info (atom {}))
(def app (atom nil))

(defn run
  []
  (let [x (api/register "🫥")]
    (reset! game-info x)

    (while true
      (Thread/sleep 1000)
      (println "runnin: " (pr-str @run?))
      ;; You probably want to get the current game-state from the server before you do your move
      (api/move (:id @game-info) (rand-nth ["LEFT" "RIGHT"])))))

(defn start
  []
  (reset! app (future (run))))

(defn stop
  []
  (future-cancel @app))

(defn -main
  [& args]
  ;; game-info contains the game map
  (run))