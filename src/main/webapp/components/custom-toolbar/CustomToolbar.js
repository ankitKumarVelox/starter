Vue.component("CustomToolbar", {
  props: ["grid"],

  mixins: [VueFactory.methodMixin()],

  template: `
    <div ref="root" class="custom-toolbar flex-row ai-center">
        <slot>This is a custom toolbar</slot>
    </div>
  `,

  mounted() {
    const root = this.$refs.root;

    const anchorId = `${this.grid.$id.screenId}_${this.grid.$id.controlName}`;
    const gridEl = document.getElementById(anchorId);
    const anchorEl = gridEl.querySelector(".dg-toolbar .dg-toolbar-group.dg-toolbar-custom");

    root.remove();
    anchorEl.parentElement.insertBefore(root, anchorEl);
  },
});
