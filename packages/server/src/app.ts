import Koa from 'koa';
import Router from 'koa-router';
import logger from 'koa-logger';
import json from 'koa-json';

import { MyType, add } from 'shared';

const app = new Koa();
const router = new Router();

router.get('/', async (ctx: Koa.ParameterizedContext, next: Koa.Next) => {
  ctx.body = {
    msg: 'TERArium'
  };
  await next();
});

router.get('/test', async (ctx: Koa.ParameterizedContext, next: Koa.Next) => {
  const test: MyType = {
    name: 'test'
  };
  let a = add(1, 2);
  ctx.body = {
    msg: test.name + ", " + a
  };
  await next();
});

app.use(json());
app.use(logger());
app.use(router.routes()).use(router.allowedMethods());

app.listen(3000, () => {
  console.log('Server started');
});
