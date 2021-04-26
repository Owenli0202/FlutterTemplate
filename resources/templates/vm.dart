import 'package:get/get.dart';
import 'package:thailand/common/framework/base/base_vm.dart';
import 'package:thailand/common/net/api.dart';
import 'package:thailand/common/net/result_data.dart';

import 'state.dart';

class $nameVM extends BaseVM<$nameState> {

  $nameVM();

  @override
  void onInit() {
    state = $nameState();
    super.onInit();
  }

  void getArguments() {
    Object arguments = Get.arguments;
  }

  @override
  void getData() async {
    showLoading();
    ResultData result = await httpManager.fetch('', {});
    if (result.success) {
      showResult();
    } else {
      showError(result.code, result.message);
    }
  }
}
