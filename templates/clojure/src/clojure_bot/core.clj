(ns clojure-bot.core
  (:require [clojure-bot.api :as api])
  (:gen-class))

(def game-info (atom {}))
(def app (atom nil))
(def game-state-atom (atom {}))

(defn get-game-state
  []
  (let [game-state (api/game-state)]
    (reset! game-state-atom game-state)))

(defn distance [p0 p1]
  (println "p0: " p0 "p1: " p1)
  (+ (Math/abs ^int (- (:x p0) (:x p1)))
     (Math/abs ^int (- (:y p0) (:y p1)))))

(defn my-position
  [game-state name]
  (-> game-state
      :players
      (->> (filter #(= (:name %) name)))
      first
      :position))

(defn closest-item
  [game-state name]
  (let [position (my-position game-state name)]
    (-> game-state :items (->> (sort-by #(distance position (:position %)))) first)))

(def user-name "🫥")

(defn what-move
  [game-state name]
  (println "game-state: " game-state)
  (let [_ (reset! game-state-atom game-state)
        my-position (my-position game-state name)
        closest (closest-item game-state name)]
    (println "my-pos" (pr-str my-position) "closest: " (pr-str (:position closest)))
    (if (< (:x my-position) (-> closest :position :x))
      "RIGHT"
      "LEFT")))

(defn run
  []
  (let [x (api/register user-name)]
    (reset! game-info x)

    (while true
      (println "cycle")
      (Thread/sleep 1000)
      (let [game-state (api/game-state)]
        (api/move (:id @game-info) (what-move game-state user-name)))

      ;; You probably want to get the current game-state from the server before you do your move
      )))

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