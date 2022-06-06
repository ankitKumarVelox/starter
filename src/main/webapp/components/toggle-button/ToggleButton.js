Vue.component("ToggleButton", {
  props: ["vm"],
  template: `<div class="toggle-button flex-row ai-center gap-between" style="padding-left: 5px">
                <label class="switch">
                    <vx-checkbox :vm="vm" type="checkbox"/>
                    <span class="slider"></span>
                </label>
                <span class="text flex-1"><slot></slot></span>
            </div>`,
});
