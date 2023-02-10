(ns clojure-bot.core
  (:require [clojure-bot.api :as api]
            [clojure.set :as set]
            [astar.core :as astar])
  (:gen-class))

(def game-info (atom {}))
(def app (atom nil))
(def game-state-atom (atom {}))

(defn map-response->map [{:keys [width height tiles] :as m}]
  (into #{}
        (filter (fn [[x y]]
                  (= \_ (nth (nth tiles y) x \X)))
                (reduce concat
                        (for [y (range height)]
                          (for [x (range width)]
                            [x y]))))))

(defn get-game-state
  []
  (let [game-state (api/game-state)]
    (reset! game-state-atom game-state)))

(defn distance [p0 p1]
  (+ (Math/abs ^int (- (:x p0) (:x p1)))
     (Math/abs ^int (- (:y p0) (:y p1)))))

(defn my-user
  [game-state name]
  (-> game-state
      :players
      (->> (filter #(= (:name %) name)))
      first))

(defn my-position
  [game-state name]
  (:position (my-user game-state name)))

(defn closest-item
  [items position graph]
  (-> items (->> (sort-by #(distance position (:position %)))) first))

(def user-name "🫥")

(defn neighbours [position game-map]
  (let [{:keys [x y]} position
        candidates #{[(inc x) y]
                     [(dec x) y]
                     [x (inc y)]
                     [x (dec y)]}]
    (set/intersection candidates game-map)))


(defn map-graph
  [game-map]
  (let [positions (vec game-map)]
    (zipmap positions (map
                        #(vec
                           (neighbours
                             {:x (nth % 0) :y (nth % 1)}
                             game-map))
                        positions))))

(defn dist
  [[x1 y1] [x2 y2]]
  (+ (Math/abs ^int (- x1 x2))
     (Math/abs ^int (- y1 y2))))

(defn route [graph start goal]
  (astar/route graph dist (partial dist goal) start goal))

(defn closest-item-by-route
  [items {:keys [x y]} graph]
  (-> items
      (->> (sort-by
            (fn [item]
              (count (route graph [x y] [(:x (:position item))
                                         (:y (:position item))])))))
      first))

(defn what-move
  [game-state name game-map graph]
  (let [_ (reset! game-state-atom game-state)
        my-health (:health (my-user game-state name))
        my-position (my-position game-state name)
        my-pos-cords [(:x my-position) (:y my-position)]
        my-pos-x (:x my-position)
        my-pos-y (:y my-position)
        beers (filter
                #(= (:type %) "POTION")
                (:items game-state))
        non-beers (filter
                    #(not= (:type %) "POTION")
                    (:items game-state))
        closest
        (closest-item-by-route (if (> my-health 50)
                                 non-beers
                                 beers)
                               my-position
                               graph)
        closest-coords (let [{:keys [x y]} (:position closest)]
                         [x y])

        first-move (first (astar/route graph dist (partial dist closest-coords) my-pos-cords closest-coords))
        _ (println "route: " (astar/route graph dist (partial dist closest-coords) my-pos-cords closest-coords))]
    #_(println "my-pos" (pr-str my-position) "closest: " (pr-str (:position closest)))


    (let [xdif (- (-> closest :position :x) (:x my-position))
          ydif (- (-> closest :position :y) (:y my-position))
          xdist (Math/abs ^int xdif)
          ydist (Math/abs ^int ydif)
          move (cond (nil? first-move)
                     "PICK"
                     (= [(inc my-pos-x) my-pos-y] first-move)
                     "RIGHT"
                     (= [my-pos-x (inc my-pos-y)] first-move)
                     "UP"
                     (= [(dec my-pos-x) my-pos-y] first-move)
                     "LEFT"
                     (= [my-pos-x (dec my-pos-y)] first-move)
                     "DOWN")
          _ (println "move: " move)]
      move)))

(defn run
  []
  (let [x (api/register user-name)
        game-map (-> (api/game-map)
                     map-response->map)
        graph (map-graph game-map)]
    (reset! game-info x)

    (while true
      (println "cycle")
      (Thread/sleep 333)
      (let [game-state (api/game-state)]
        (api/move (:id x) (what-move game-state user-name game-map graph)))

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
