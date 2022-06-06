Vue.component("PopOver", {
  props: ["selector"],
  mixins: [VueFactory.methodMixin()],
  template: `
    <div class="pop-over" ref="root" style="z-index: 999; height: auto; width: auto; box-shadow: 1px 1px 7px; border-radius: 4px">
      <div ref="arrow" class="arrow"></div>
      <slot></slot>
    </div>
  `,

  data() {
    return {popper: null, onClickHandler: null};
  },

  mounted() {
    const root = this.$refs.root;
    const arrow = this.$refs.arrow;
    const anchorEl = document.querySelector(this.selector);
    this.popper = Popper.createPopper(anchorEl, root, {
      placement: "bottom",
      modifiers: [
        {
          name: "offset",
          options: {
            offset: [0, 2]
          }
        },
        {
          name: 'preventOverflow',
          options: {
            padding: 8
          }
        },
        {
          name: "arrow",
          options: {
            element: arrow,
            padding: 5
          }
        }
      ]
    });
    this.onClickHandler = ev => this.onClick(ev);
    document.addEventListener("click", this.onClickHandler, {capture: true});
  },

  destroyed() {
    this.popper.destroy();
    document.removeEventListener("click", this.onClickHandler, {capture: true});
    this.onClickHandler = null;
  },

  methods: {
    onClick(ev) {
      if (!ev.target || !this.$refs.root.contains(ev.target)) {
        this.$emit("click-outside");
      }
    }
  }


});
