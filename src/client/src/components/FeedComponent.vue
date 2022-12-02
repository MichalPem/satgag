<template>
  <q-list id="scrollTargetRef" ref="scrollTargetRef" :style="isComment ? '': 'max-height: 100%;overflow: auto;'" class="feed q-mr-sm q-ml-sm" separator>
    <q-infinite-scroll :offset="100" :scroll-target="scrollTargetRef" @load="onLoadMenu">
      <div v-for="a in feed" :id="a.id" v-bind:key="a.meme_id" class="row" style="scroll-margin-top: 50px;" >
        <ArticleComponent  :article="a"
                           :is-comment="isComment"
                           :is-feed="true"
                           @sat="()=>$emit('sat')"
                           @article-changed="(article) => $emit('articleChanged',article)"
        />
      </div>

      <template v-slot:loading>
        <div class="text-center q-my-md">
          <q-spinner-dots color="primary" size="40px" />
        </div>
      </template>
    </q-infinite-scroll >
  </q-list>
</template>

<script>

import ArticleComponent from "components/ArticleComponent";
import axios from "axios";
import {useCounterStore} from "stores/example-store";
import {ref} from "vue";
import {useArticleStore} from "stores/article-store";
export default {
  name: "FeedComponent",
  emits: ['articleChanged','sat'],
  components: {ArticleComponent},
  props: [
    "feed",
    "disableScroll",
    "isComment",
    "isGeneralFeed"
  ],
  data : function () {
    return {

    }
  },
  // watch: {
  //   // whenever question changes, this function will run
  //   feed(newArticle, oldQuestion) {
  //
  //   }
  // },
  methods : {
    // update()
    // {
    //   if(this.store.article && !this.store.switchInProgress)
    //     this.store.fetchArticles()
    // },
    scroll(id) {
      document.getElementById(id).scrollIntoView({
        behavior: "auto"
      })
    },
    onLoadMenu (index, done) {

      if (this.store.article === null || this.store.switchInProgress) {
        setTimeout(()=>{done()},500)
      } else {
        this.store.nextPage().then(() => {

          done(this.store.finished)

        })
      }


      // if (index > 1 && !this.store.finished) {
      //
      // }
      // else {
      //   setTimeout(() => {
      //     done()
      //   }, 500)
      // }
    },

  },

  mounted() {
    // let a = this.isComment
    // let b = this.isGeneralFeed
    // debugger
    // window.addEventListener("keydown", e => {
    //   if(e.key === 'j' && this.store.current_scroll_id+1 < this.store.articles.length)
    //   {
    //     this.store.current_scroll_id += 1
    //     this.scroll(this.store.articles[this.store.current_scroll_id].memeId)
    //   } else if (e.key === 'k' && this.store.current_scroll_id > 0)
    //   {
    //     this.store.current_scroll_id -=1;
    //     this.scroll(this.store.articles[this.store.current_scroll_id].memeId)
    //   }
    // });

  },
  setup () {
    const store = useArticleStore()
    const scrollTargetRef = ref(null)
    return {
      store,
      scrollTargetRef,
    }
  }
}
</script>

<style scoped>

</style>
