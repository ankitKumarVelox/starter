Vue.component("TabFlasher", {
  props: ["vm", "selector"],

  mixins: [VueFactory.methodMixin()],

  data: function () {
    return {timer: 0};
  },

  template: `<div ref="root" :class="['tab-flasher', vm.$id.controlName]" style="display: none"></div>`,

  watch: {
    "vm.Value": function () {
      const value = this.vm.Value;
      if (value > 0) {
        if (this.timer > 0) {
          clearTimeout(this.timer);
          this.timer = 0;
        }

        const root = this.$refs.root;
        const layoutRoot = root.closest(".lm_goldenlayout.lm_root");
        layoutRoot?.querySelectorAll(this.selector)?.forEach(it => {
          if (!it.classList.contains("flashing")) {
            it.classList.add("flashing");
          }
        });

        this.timer = setTimeout(() => this.stopFlashing(), 10000);
      }
    },
  },

  methods: {
    stopFlashing() {
      const root = this.$refs.root;
      const layoutRoot = root.closest(".lm_goldenlayout.lm_root");
      const targets = layoutRoot.querySelectorAll(".flashing");
      targets.forEach(it => it.classList.remove("flashing"));
      this.timer = 0;
    },
  },
});



