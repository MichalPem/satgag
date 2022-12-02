<template>
  <q-page class="bg-black" style="overflow: scroll;height: 100%">
    <ArticleComponent
      :article="store.article"
      :is-comment="false"
      @sat="()=>$emit('sat')"
      @send-comment="store.submit"
    />
    <FeedComponent
      id="scroller"
      ref="feedComponent"
      :class="(store.article !== null && store.article.date !== undefined) ? 'q-ml-xl':''"
      :feed="store.feed"
      :is-comment="store.article !== null && store.article.date !== undefined"
      :isGeneralFeed="store.article !== null && (store.article.id === 'new' || store.article.id === '')"
      @sat="()=>$emit('sat')"
      @article-changed="open"/>
  </q-page>
</template>

<script>
import { defineComponent } from 'vue'

import { ref } from 'vue'
import FeedComponent from "components/FeedComponent";
import {useCounterStore} from "stores/example-store";
import ArticleComponent from "components/ArticleComponent";
import {useArticleStore} from "stores/article-store";
import {base58_to_binary, binary_to_base58} from "base58-js";

export default defineComponent({
  name: 'IndexPage',
  components: {ArticleComponent, FeedComponent},
  props : [
    "id"
  ],
  emits: ['sat'],
  data : function () {
    return {
      fr:null,
    }
  },
  methods : {
    open(article)
    {

      let long = article.id;
      let byteArray = [0, 0, 0, 0, 0, 0, 0, 0];

      for ( var index = 0; index < byteArray.length; index ++ ) {
        var byte = long & 0xff;
        byteArray [ index ] = byte;
        long = (long - byte) / 256 ;
      }

      let a = binary_to_base58(byteArray)
      this.store.history.push({
        article : this.store.article,
        feed : this.store.feed,
        page : this.store.page,
        scroll: article.id
      })
      this.$router.push('/a/'+a)

    },

  },
  mounted() {
    // let a = this.store.article
    // let b = this.store.article !== null && this.store.article.date === undefined
    // debugger
    // this.store.selectFeed(this.id === undefined ? ('') : this.toId(this.id))
  },

  setup () {
    const store = useArticleStore()

    return {
      store,
    }
  }

})
</script>
