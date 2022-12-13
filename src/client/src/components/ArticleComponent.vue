<template>
  <q-item-section v-if="article && article.date" class="col q-mb-sm " >
    <div :class="isComment ? '':'text-caption'" class="row q-mb-sm text-white" >
      <q-icon :name="article.user.imgurl?'img:'+article.user.imgurl:'account_circle'" class="q-mr-sm" size="sm"/>

      {{ article.user.nick ? article.user.nick : '@'+article.user.lnid }}
    </div>
    <div :class="isComment ? '':'text-h6'" class="row q-mb-sm text-white cursor-pointer" @click="$emit('articleChanged',article)">{{ article.title }}</div>
    <div class="row full-width q-mb-sm cursor-pointer" @click="$emit('articleChanged',article)">
      <img v-if="imagedata" :class="isComment ? '' : 'full-width'" :src="imagedata" :style="isComment ? 'width: 50%':''">
    </div>
    <div class="row items-center q-mb-sm cursor-pointer" >
      <span class="q-mr-sm">{{ article.sats }} sats</span>
      <q-btn  color="dark" size="sm" text-color="accent" @click="sat">
        <q-icon name="electric_bolt"   />
      </q-btn>
      <q-btn  class="q-ml-sm" color="dark" size="sm" text-color="secondary"  @click="$emit('articleChanged',article)" >
        <q-icon name="comment" />
      </q-btn>
      <q-btn  class="q-ml-sm" color="dark" size="sm" text-color="secondary"  @click="share()" >
        <q-icon name="share" />
      </q-btn>
    </div>
<!--    <FeedComponent v-if="id" :articles="selected.replies"/>-->
    <q-separator  />
    <div v-if="!isFeed && (isComment || !isGeneralFeed)" class="row q-ml-xl q-mr-sm">
      <q-input v-model="text"
               autogrow
               bg-color="secondary"
               class="col"
               filled
               hint="Comment"
               label="Comment"
      />

    </div>
    <div v-if="!isFeed && (isComment || !isGeneralFeed)" class="row items-center q-mb-sm q-ml-xl">
      <div :style="imageData !== null ? { 'background-image': `url(${imageData})`,'width':'100px','height':'150px','background-size':'contain' } : ''" @click="choosepicture">

        <input ref="fileInput" accept="image/*" class="file-input" hidden type="file" @input="onSelectFile"/>
      </div>
    </div>
    <div v-if="!isFeed && (isComment || !isGeneralFeed)" class="row items-center q-mb-sm q-ml-xl">

      <q-btn class="q-mr-sm" color="primary" icon="image" size="sm" @click="chooseFile"/>
      <q-btn  color="dark" size="sm" text-color="accent"  @click="$emit('sendComment',article.id,text,image,clean)">
        <q-icon name="electric_bolt"  />
      </q-btn>
    </div>
  </q-item-section>
</template>

<script>
import {useCounterStore} from "stores/example-store";
import axios from "axios";
import {isMobile} from "mobile-device-detect";

import {base58_to_binary,binary_to_base58} from "base58-js";
import {useArticleStore} from "stores/article-store";
import constants from "src/constants";
import {copyToClipboard, useQuasar} from "quasar";

import ImageSelector from "components/ImageSelector";

export default {
  components: {

  },
  emits: ['articleChanged','sendComment','sat'],
  props: [
    "article",
    "isComment",
    "isFeed",
    "isGeneralFeed"
  ],
  name: "ArticleComponent",
  data: function () {
    return {
      mobile: isMobile,
      imagedata:null,
      imageData: null,
      image:null,
      componentKey: 0,
      text:""
    }
  },
  computed : {

  },
  mounted() {
// let a = this.store.article !== null && this.store.article.date !== undefined
//     let a = this.article
//       let b = this.isComment
//       debugger
   if(this.article !== null && this.article.memeId !== undefined) this.loadImage(this.article.memeId).then((image)=>this.imagedata=image)
  },
  watch: {
    // whenever question changes, this function will run
    article(newArticle, oldQuestion) {
    // let a = this.article
    //   let b = this.isComment
    //   debugger
      if(this.article !== null && this.article.memeId !== undefined) this.loadImage(newArticle.memeId).then((image)=>this.imagedata=image)
    }
  },
  methods : {
    share()
    {
      copyToClipboard(constants.host + '/a/'+this.store.binaryToBase58(this.article.id))
        .then(()=>this.$q.notify('Link copied!'))
    },
    clean()
    {
      this.text = ""
      this.imageData = null
      this.image = null
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
    choosepicture() {
      this.$refs.fileInput.click();
    },
    // onSubmit()
    // {
    //   this.store.submit(this.article.id,this.text,this.image,()=> {
    //     this.text = ""
    //     this.imageData = null
    //     this.image = null
    //     this.store.loadArticle(this.aid)
    //   })
    // },
    async loadImage(id)
    {

      if(id===null)return null;
      if(this.store.imageCache.get(id) !== null && this.store.imageCache.get(id) !== undefined)
      {
        return window.URL.createObjectURL((this.store.imageCache.get(id)));
      }
      const response = await axios.get(constants.host+'/api/img/'+id, {
        responseType: 'blob'
      })
      let b = new Blob([response.data], {type: 'image/jpeg'});
      this.store.imageCache.set(id,b)
      return window.URL.createObjectURL(b)
    },
    sat() {
      if(!this.store.user || !this.store.user.password)
      {
        this.$q.notify({
          message: "Please login or register",
          type:"negative",
          timeout: 2500,
        })
        this.$router.push("/profile")
        return;
      }
      if(this.store.user.balance <= 0)
      {
        this.$q.notify({
          message: "Please top up your wallet",
          type:"negative",
          timeout: 2500,
        })
        return;
      }

      this.store.user.balance -= 1
      this.$emit('sat')
      axios.patch(constants.host+"/api/pay/"+this.store.user.password,null, { params: {
          to:this.article.user.id,
          article_id:this.article.id,
          amount:this.store.user.satAmount
        }}).then((response)=>{
          this.store.modifyBalance(this.article.id,response.data.sats)
      }).catch((e)=>{
        this.$q.notify({
          message: e.response.data ? e.response.data : "Error",
          type:"negative",
          timeout: 2500,
        })
      })
    }
  },
  setup () {
    const store = useArticleStore()
    return {
      store,
    }
  }
}
</script>

<style scoped>

</style>
