import Koa from 'koa';
import Router from 'koa-router';
import logger from 'koa-logger';
import json from 'koa-json';

const app = new Koa();
const router = new Router();

router.get('/', async (ctx: Koa.ParameterizedContext, next: Koa.Next) => {
  ctx.body = {
    msg: 'TERArium'
  };
  await next();
});

app.use(json());
app.use(logger());
app.use(router.routes()).use(router.allowedMethods());

app.listen(3000, () => {
  console.log('Server started');
});
