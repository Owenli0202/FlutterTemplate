import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:thailand/common/framework/base/base_view.dart';

import 'vm.dart';
import 'state.dart';

class $nameView extends BaseView<$nameVM, $nameState> {
  void init(BuildContext context) {
    Get.put($nameVM());
  }

  @override
  Widget getContent(BuildContext context) {
    return Obx(() {
      return Column(
        children: [
        ],
      );
    });
  }
}
