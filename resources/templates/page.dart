import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:thailand/common/framework/base/base_page.dart';
import 'package:thailand/common/widgets/action_bar/action_bar.dart';

import 'vm.dart';
import 'state.dart';

class $namePage extends BasePage<$nameVM, $nameState> {
  void init(BuildContext context) {
    Get.put($nameVM());
  }

  @override
  Widget getContent(BuildContext context) {
    return Obx(() {
      return Column(
        children: [
          ActionBar(),
          Expanded(child: null),
        ],
      );
    });
  }
}
