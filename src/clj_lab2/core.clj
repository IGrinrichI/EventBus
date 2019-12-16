(ns clj-lab2.core)
(require '[clojure.core.async :as async :refer [<! >! go go-loop chan]])

;Контейнер с пользователями (Имя, Канал)
(def s-chan (agent {}))

;Действие, которое совершается у Подписчиков
(def on-event (fn [sub event] (println sub " get " event)))

;Создать нового пользователя с именем name
(def new-user (fn [name] (send s-chan assoc name (chan))
                (go-loop []
                   (let [event (<! (@s-chan name))]
                     (on-event name event)
                     (recur)))))

;Ассоциативный контейнер Публикующий - Подписчики
(def sp-map (agent {}))

;Каналы входящих постов
(def posts (chan 10))          ;{:publisher "pablo" :event "message"}

;Создать пост
(def create-post (fn [publisher event]
                   (go
                     (>! posts {:publisher publisher :event event}))))

;Подписаться на Публикующего
(def subscribe-to (fn [sub pub] (send sp-map update pub conj sub)))

;Рассылка события по Подписчикам
(def post (fn [some-post]
                   (loop [sub (@sp-map (some-post :publisher))]
                     (when (not (nil? (first sub)))
                       (go (>! (@s-chan (first sub)) (some-post :event)))
                       (recur (rest sub))
                       ))))

;Реагирует на входящие посты, и вызывает рассылку по Подписчикам
(go-loop []
  (let [some-post (<! posts)]
    (post some-post)
    )(recur))

;(def my-map (agent {"publisher1" ["sub1","sub2"]}))
;(send my-map update "publisher2" conj "sub5")
;(send my-map assoc "publisher2" ["sub3","sub4"])