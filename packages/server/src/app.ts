import Koa from 'koa';
import Router from 'koa-router';
import logger from 'koa-logger';
import json from 'koa-json';
import serve from 'koa-static';

import { MyType, add } from 'shared'; // eslint-disable-line

const port = 3000;
const app = new Koa();
const router = new Router();

app.use(serve('dist/web'));

router.get('/api', async (ctx: Koa.ParameterizedContext, next: Koa.Next) => {
  ctx.body = {
    API: {
      description: 'This is the root of TERArium API',
      routes: {},
    },
  };
  await next();
});

router.get(
  '/api/test',
  async (ctx: Koa.ParameterizedContext, next: Koa.Next) => {
    const test: MyType = {
      name: 'test',
    };
    const a = add(1, 2);
    ctx.body = {
      msg: `${test.name}, ${a}`,
    };
    await next();
  }
);

app.use(json());
app.use(logger());
app.use(router.routes()).use(router.allowedMethods());

app.listen(port, () => {
  console.log(`Server started on ${port}`); // eslint-disable-line
});
