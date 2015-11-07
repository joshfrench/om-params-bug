(ns om-tutorial.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(def init-data
  {:dashboard/posts
   [{:id 0 :favorites 0}]})

(defui Post
  static om/Ident
  (ident [this {:keys [id]}]
    [:post/by-id id])

  static om/IQuery
  (query [this]
    [:id :favorites])

  Object
  (render [this]
    (let [{:keys [id favorites] :as props} (om/props this)]
      (dom/div nil
        (dom/p nil "Favorites: " favorites)
        (dom/button
            #js {:onClick
                 (fn [e]
                   (om/transact! this
                     `[(post/favorite {:id ~id})]))}
            "Favorite!")))))

(def post (om/factory Post))

(defui Dashboard
  static om/IQuery
  (query [this]
    ;; `[{:dashboard/posts ~(om/get-query Post)}] ;; works fine
    `[({:dashboard/posts ~(om/get-query Post)} {:foo "bar"})] ;; "No queries exist for component path (om-tutorial.core/Dashboard om-tutorial.core/Post) or data path [:dashboard/items]"
    )

  Object
  (render [this]
    (let [{:keys [dashboard/posts]} (om/props this)]
      (apply dom/ul nil
        (map post posts)))))

(defmulti read om/dispatch)

(defmethod read :dashboard/posts
  [{:keys [state selector]} k _]
  (let [st @state]
    {:value (om/db->tree selector (get st k) st)}))

(defmulti mutate om/dispatch)

(defmethod mutate 'post/favorite
  [{:keys [state]} k {:keys [id]}]
  {:action
   (fn []
     (swap! state update-in [:post/by-id id :favorites] inc))})

(def reconciler
  (om/reconciler
    {:state  init-data
     :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler Dashboard (gdom/getElement "app"))
