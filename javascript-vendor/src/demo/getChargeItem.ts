import { aidbox } from './index';

async function getChargeItem() {
  let chargeItemsRequest = aidbox
    .getResources('ChargeItem')
    .where('factor-override', 1e2)
    // .where('factor-override', [1, 2], 'ne')
    .sort([{ key: 'factor-override', dir: 'acs' }])
    .elements(['factorOverride']);

  let response = await chargeItemsRequest;
}

getChargeItem();
