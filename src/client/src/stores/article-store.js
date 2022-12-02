import { defineStore } from 'pinia';

import axios from "axios";
import { Cookies } from 'quasar'
import { inject } from 'vue'


import constants from "src/constants";

export const useArticleStore = defineStore('article-store', {
  state: () => ({
    article:null,
    feed:[],
    page: 0,
    finished:false,
    switchInProgress: false,
    user:null,
    history:[],
    imageCache: new Map(),
  }),
  getters: {
    isComment: (state)=> {
      return state.article !== null && state.article.date !== undefined
    },
    verify:(state)=> {
      if(state.user.password) {
        axios.get(constants.host + "/api/login/" + state.user.password).then((response) => {
          state.user = response.data
          Cookies.set('prkey', state.user.password, {
            secure: true,
            expires: 10
          })

        })
      }
    },
  },
  actions: {
    async selectFeed(id,callback) {

      this.switchInProgress = true
      let article = null;
      if (id === "new" || id === '') {
        article = {
          id: id
        }
      } else {
        let response = await axios.get(constants.host + "/api/a/" + id)
        article = response.data
      }
      this.page = 0
      this.fetchArticles(article).then((feed) => {
        this.switchInProgress = false
        this.feed = feed

        this.finished = false
        this.article = article
        if(document.getElementById('scroller')) document.getElementById('scroller').scrollTo(0,0)
      })

    },
    modifyBalance(id, newbalance) {
      let a = this.feed.filter(a => a.id === id)
      if (a.length > 0)
        a[0].sats = newbalance
      else if (id === this.article.id) {
        this.article.sats = newbalance
      }
    },
    async nextPage() {

      this.page += 1;
      let feed = await this.fetchArticles(this.article)
      this.switchInProgress = false

      if (feed.length === 0) {
        this.finished = true;
        return
      }
      feed.forEach(a => {
        this.feed.push(a)
      })
      this.finished = false
    },
    async fetchArticles(article) {
      try {

        let feed = []
        const response = await axios.get(constants.host + "/api/feed/" + (article !== null ? article.id : null) + "?page=" + this.page)
        if (response.data.length === 0) this.finished = true;
        response.data.forEach(a => {
          feed.push(a)
        })
        return feed
      } catch (error) {
        this.finished = true;
        console.log(error)
      }
    },
    async submit(parent, title, image, callback,errorCallback) {
      if ((title === null || title === "") && image === null) return;
      // this.$q.loading.show();

      try {

      let formData = new FormData();
      // let c = 0
      // this.files.forEach(f => {
      //   formData.append("files", f);
      //   c++;
      // })
      formData.append("file", image);
      formData.append("parent", parent === null ? -1 : parent);
      formData.append("title", title);
      let r = await axios.post(constants.host + '/api/save/' + this.user.password, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
        // this.$q.loading.hide();
        this.feed.unshift(r.data);
      callback(r.data.id)
      // this.onReset()
    } catch(error)
{
  //}).catch((error)=>{
  if (error.response) {
    errorCallback(error.response.data)

    // Request made and server responded
    console.log(error.response.data);
    console.log(error.response.status);
    console.log(error.response.headers);
  } else if (error.request) {
    // The request was made but no response was received
    console.log(error.request);
  } else {
    // Something happened in setting up the request that triggered an Error
    console.log('Error', error.message);
  }
  // this.$q.loading.hide();
  // this.$q.notify({
  //   type: 'negative',
  //   message: "Chyba pri ukladani"
  // })
}
    },
    goBack()
    {
      if(this.history.length > 0)
      {
        var h = this.history.pop();
        this.article = h.article;
        if(h.feed.length > 30)
        {
          this.feed = h.feed.slice(h.feed.length-28,h.feed.length-1)
        } else {
          this.feed = h.feed;
        }
        this.page = h.page;
        setTimeout(()=>{
          document.getElementById(h.scroll).scrollIntoView({
            behavior: "auto"
          })

        },500)


      }
    }
  }
});
