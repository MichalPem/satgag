<template>
  <q-page class="bg-black">
    <FeedComponent />
  </q-page>
</template>

<script>
import { defineComponent } from 'vue'
import FeedComponent from "components/FeedComponent";
import {useCounterStore} from "stores/example-store";

export default defineComponent({
  name: 'NewPage',
  components: {FeedComponent},
  mounted() {
    this.store.setFeed("new")
  },
  setup () {
    const store = useCounterStore()
    return {
      store,
    }
  }
})
</script>
