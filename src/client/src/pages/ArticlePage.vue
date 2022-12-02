<template>
  <q-page class="bg-black " style="max-height: 100vh;overflow: scroll" >
    <div class="row q-mr-sm">
      <ArticleComponent v-if="store.selected" :article="store.selected"/>
    </div>
    <div class="row q-ml-xl q-mr-sm">
      <q-input v-model="text"
               autogrow
        bg-color="secondary"
               class="col"
               filled
               hint="Comment"
        label="Comment"
      />

    </div>
    <div class="row items-center q-mb-sm q-ml-xl">

      <q-btn class="q-mr-sm" color="primary" icon="image" size="sm" @click="chooseFile"/>
      <q-btn  color="dark" size="sm" text-color="accent" @click="onSubmit">
        <q-icon name="electric_bolt"   />
      </q-btn>
    </div>
    <q-card-section class="q-ml-xl">
      <img :src="imageData" width="100">
      <input ref="fileInput" class="file-input" hidden type="file" @input="onSelectFile" />
    </q-card-section>
    <FeedComponent v-if="store.selected" :disable-scroll="true" :is-comment="true" class="q-ml-xl" @article-changed="open"/>
  </q-page>
</template>

<script>
import ArticleComponent from "components/ArticleComponent";
import FeedComponent from "components/FeedComponent";
import {base58_to_binary, binary_to_base58} from "base58-js";
import axios from "axios";
import {useCounterStore} from "stores/example-store";
export default {
  name: "ArticlePage",
  props : [
    "id"
  ],
  data : function () {
    return {
      text:"",
      imageData: null,
      image:null,
    }
  },
  mounted() {

    if(this.article === undefined && this.id !== undefined)
    {
      let b = base58_to_binary(this.id);
      let id = 0;
      for (var i = b.length - 1; i >= 0; i--) {
        id = (id * 256) + b[i];
      }
      this.aid = id;
      this.store.setFeed(id)
      this.store.loadArticle(id)
    }
  },
  watch: {
    'selected': function () {
      debugger
      if(this.article === undefined && this.id !== undefined)
      {
        let b = base58_to_binary(this.id);
        let id = 0;
        for (var i = b.length - 1; i >= 0; i--) {
          id = (id * 256) + b[i];
        }
        this.aid = id;
        this.store.setFeed(id)
        this.store.loadArticle(id)
      }
    }
  },
  methods:  {
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
      this.store.selected = article
      this.$router.push({name: 'article',params : { a }})
      this.store.loadArticle(article.id)
    },
    chooseFile(){
      this.$refs.fileInput.click();
    },
    onSelectFile() {
      const input = this.$refs.fileInput;
      const files = input.files;
      this.image = files[0];
      if (files && files[0]) {
        const reader = new FileReader();
        reader.onload = e => {
          this.imageData = e.target.result;
        };
        reader.readAsDataURL(files[0]);
        this.$emit("input", files[0]);
      }
    },
    onSubmit()
    {
      this.store.submit(this.aid,this.text,this.image,()=> {
        this.text = ""
        this.imageData = null
        this.image = null
        this.store.loadArticle(this.aid)
      },(e)=>{
        this.$q.notify({
          type: 'negative',
          message: e !== null ? e :"Error when saving meme. Not enough sats?"
        })
      })
    },
  },
  components: {FeedComponent, ArticleComponent},
  setup () {
    const store = useCounterStore()
    return {
      store,
    }
  }
}
</script>

<style scoped>

</style>
