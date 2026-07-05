import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-account',
  template: `
    <h1>Account {{ accountId() }}</h1>
    <p>Account overview — to be implemented.</p>
  `,
})
export class AccountComponent {
  private readonly route = inject(ActivatedRoute);

  readonly accountId = toSignal(this.route.paramMap.pipe(map((params) => params.get('id'))));
}
