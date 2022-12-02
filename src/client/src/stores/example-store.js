import { defineStore } from 'pinia';
import axios from "axios";
import { Cookies } from 'quasar'

export const useCounterStore = defineStore('counter', {
  state: () => ({
    $q:null,
    page: 0,
    feed:"",
    articles: [],
    finished:false,
    selected:null,
    backend:"localhost:9080",
  }),

  getters: {
    host:(state) => {
      console.log(state.backend.indexOf("localhost"))
      if(state.backend.indexOf("localhost")>-1)
      {
        return "http://"+state.backend;
      }
      return state.backend;
    },
    verify:(state)=> {
      if(state.user.password) {
        axios.get(state.host + "/api/login/" + state.user.password).then((response) => {
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
    async loadArticle(id) {
      this.articles = []
      if(id===null)return null;
      let response = await  axios.get(this.host + "/api/a/"+id)
      //   .catch(()=>{
      //   this.$q.notify({
      //     timeout: 2500,
      //     type: 'negative',
      //     message:"not found",
      //   })
      // })
      if(this.selected === null) {
        this.selected = response.data
        return
      } else {
        this.selected.id = response.data.id
        this.selected.memeId = response.data.memeId
        this.selected.title = response.data.title
        this.selected.balance = response.data.balance
      }

      this.setFeed(id)
    },
    async loadImage(id)
    {
      if(id===null)return null;
      const response = await axios.get(this.host+'/api/img/'+id, {
        responseType: 'blob'
      })
      return window.URL.createObjectURL(new Blob([response.data], {type: 'image/jpeg'}))
    },
    submit(parent,title,image,callback) {

      if((title === null || title === "") && image === null) return;

      let formData = new FormData();
      // let c = 0
      // this.files.forEach(f => {
      //   formData.append("files", f);
      //   c++;
      // })
      formData.append("file", image);
      formData.append("parent", parent);
      formData.append("title",title);
      axios.post(this.host+'/api/save/'+this.user.password, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then( (response) => {

        callback()
        // this.onReset()
      }).catch((error)=>{
        if (error.response) {
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
      })
    },
    setFeed(feed) {
      this.feed = feed;
      this.finished = false;
      this.page = 0
      this.articles = []
      this.fetchArticles()
    },
    modifyBalance(id,newbalance)
    {
      let a = this.articles.filter(a => a.id === id)
      if(a.length > 0)
        a[0].sats = newbalance
      else if(id === this.selected.id)
      {
        this.selected.sats = newbalance
      }
    },
      nextPage() {
        this.page += 1;

        this.fetchArticles()
      },
      async fetchArticles() {
        try {

          const response = await axios.get(this.host + "/api/article/"  +this.page+"?feed="+(this.feed!==undefined?this.feed:null))
          if( response.data.length === 0) this.finished = true;
          response.data.forEach(a => {
            this.articles.push(a)
          })
        }
        catch (error) {
          console.log(error)
        }
      }
  },
});
