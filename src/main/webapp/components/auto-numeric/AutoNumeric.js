Vue.component("AutoNumeric", {
  props: ["vm", "options"],

  mixins: [VueFactory.methodMixin()],

  data: function () {
    return { autonumeric: null };
  },

  template: `<input ref="root"
      :class="['auto-numeric', vm.$id.controlName]"
      @input="onInput" 
      :disabled="!vm.Enabled" 
      v-show="vm.Visible">
    `,

  mounted() {
    this.autonumeric = new AutoNumeric(this.$refs.root, this.options);
  },

  destroyed() {
    this.autonumeric.nuke();
  },

  watch: {
    "vm.Value": function () {
      const value = this.autonumeric.getNumber();
      if (value !== this.vm.Value) {
        this.autonumeric.set(this.vm.Value);
      }
    },
  },

  methods: {
    onInput(e) {
      const value = e.target.value;
      this.request(this.vm, value);
    },
  },
});



