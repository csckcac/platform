package org.wso2.licensemgt.model{
import mx.controls.Text;
        public class DgToolTipUtil extends Text
            {
        public function DgToolTipUtil() {
            height = 20;
              }
        override public function set data(value:Object):void {
            super.data = value;
           switch (this.text) {
                       case "col1":
                            this.toolTip = "Col 1 tool tip.";
                           break;

                       case "col2" :
                            this.toolTip = "Col 2 tool tip.";
                           break;

                       case "col3" :
                            this.toolTip = "Col 3 tool tip.";
                           break;

                       default :
                              this.toolTip = this.text;
                          break;
            }
            super.invalidateDisplayList();
        }
    }
}