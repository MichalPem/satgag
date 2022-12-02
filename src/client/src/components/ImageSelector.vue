<template>
  <q-card dark style="min-width: 350px">
    <q-card-section>
      <div class="text-h6" style="color: rgba(255, 255, 255, 0.55);">Post meme</div>
    </q-card-section>
    <q-card-section class="q-pt-none">
      <q-input v-model="title" autofocus bg-color="secondary" filled label="Title"  />
    </q-card-section>

    <q-card-section class="text-subitle2 ">
      <div :style="imageData !== null ? { 'background-image': `url(${imageData})` } : ''" @click="choosepicture">
              <span
                v-if="!imageData"
                class="placeholder"
                style="cursor: pointer"
              >Choose a picture</span>
        <input ref="fileInput" accept="image/*" class="file-input" hidden type="file" @input="onSelectFile" />
      </div>
      <q-img :src="imageData" style="cursor: pointer" @click="choosepicture" />
    </q-card-section>

    <q-card-actions align="right" style="color: rgba(255, 255, 255, 0.55);">
      <q-btn v-close-popup flat label="Cancel" />
      <q-btn v-close-popup flat  text-color="accent" @click="onSubmit">post (10 sats)</q-btn>
    </q-card-actions>
  </q-card>
</template>

<script>
import {isMobile} from "mobile-device-detect";
import {ref} from "vue";
import axios from "axios";
import {useArticleStore} from "stores/article-store";
import {binary_to_base58} from "base58-js";

export default {
  name: "ImageSelector",
  data: function () {
    return {
      mobile: isMobile,
      selected:"",
      imageData: null,
      image:null,
      title:"",
      parent:null
    }
  },
  methods: {
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
    onSubmit()
    {
      this.store.submit(this.parent,this.title,this.image,(id)=>{
        let long = id;
        let byteArray = [0, 0, 0, 0, 0, 0, 0, 0];

        for ( var index = 0; index < byteArray.length; index ++ ) {
          var byte = long & 0xff;
          byteArray [ index ] = byte;
          long = (long - byte) / 256 ;
        }

        let a = binary_to_base58(byteArray)
        // this.store.history.push({
        //   article : this.store.article,
        //   feed : this.store.feed,
        //   page : this.store.page,
        //   scroll: article.id
        // })
        this.$router.push('/a/'+a)
      },(e)=>{
        this.$q.notify({
          type: 'negative',
          message: e !== null ? e :"Error when saving meme. Not enough sats?"
        })
      })
    },
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
