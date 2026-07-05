import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-payment-order',
  template: `
    <h1>Payment order {{ paymentOrderId() }}</h1>
    <p>Transaction overview — to be implemented.</p>
  `,
})
export class PaymentOrderComponent {
  private readonly route = inject(ActivatedRoute);

  readonly paymentOrderId = toSignal(this.route.paramMap.pipe(map((params) => params.get('id'))));
}
