// Copyright (c) Jupyter Development Team.
// Distributed under the terms of the Modified BSD License.

import { PageConfig, URLExt } from '@jupyterlab/coreutils';
(window as any).__webpack_public_path__ = URLExt.join(
  PageConfig.getBaseUrl(),
  'notebook/'
);

import '@jupyterlab/application/style/index.css';
import '@jupyterlab/cells/style/index.css';
import '@jupyterlab/theme-light-extension/style/theme.css';
import '@jupyterlab/completer/style/index.css';
import '../index.css';

import {
  Toolbar as AppToolbar,
  SessionContext,
  SessionContextDialogs
} from '@jupyterlab/apputils';
import {
  refreshIcon,
  stopIcon,
  Toolbar,
  ToolbarButton
} from '@jupyterlab/ui-components';

import { Cell, CodeCell, CodeCellModel } from '@jupyterlab/cells';

import {
  CodeMirrorEditorFactory,
  CodeMirrorMimeTypeService,
  EditorExtensionRegistry,
  EditorLanguageRegistry,
  ybinding
} from '@jupyterlab/codemirror';

import {
  Completer,
  CompleterModel,
  CompletionHandler,
  KernelCompleterProvider,
  ProviderReconciliator
} from '@jupyterlab/completer';

import {
  standardRendererFactories as initialFactories,
  RenderMimeRegistry
} from '@jupyterlab/rendermime';

import {
  KernelManager,
  KernelSpecManager,
  SessionManager
} from '@jupyterlab/services';

import { IYText } from '@jupyter/ydoc';

import { CommandRegistry } from '@lumino/commands';

import { BoxPanel, Widget } from '@lumino/widgets';

import { getCodeBlock } from './askemlib';
import { LLMCell, LLMCellModel } from './llmcell';


function main(): void {
  const kernelManager = new KernelManager();
  const specsManager = new KernelSpecManager();
  const sessionManager = new SessionManager({ kernelManager });
  const sessionContext = new SessionContext({
    sessionManager,
    specsManager,
    name: 'TerariumNode'
  });
  const editorExtensions = () => {
    const registry = new EditorExtensionRegistry();
    for (const extensionFactory of EditorExtensionRegistry.getDefaultExtensions(
      {}
    )) {
      registry.addExtension(extensionFactory);
    }
    registry.addExtension({
      name: 'shared-model-binding',
      factory: options => {
        const sharedModel = options.model.sharedModel as IYText;
        return EditorExtensionRegistry.createImmutableExtension(
          ybinding({
            ytext: sharedModel.ysource,
            undoManager: sharedModel.undoManager ?? undefined
          })
        );
      }
    });
    return registry;
  };
  
  const languages = new EditorLanguageRegistry();
  EditorLanguageRegistry.getDefaultLanguages()
    .filter(language =>
      ['ipython', 'julia', 'python'].includes(language.name.toLowerCase())
    )
    .forEach(language => {
      languages.addLanguage(language);
    });

  const factoryService = new CodeMirrorEditorFactory({
    extensions: editorExtensions(),
    languages
  });
  const mimeService = new CodeMirrorMimeTypeService(languages);

  // Initialize the command registry with the bindings.
  const commands = new CommandRegistry();
  const useCapture = true;

  const parentIframe = window.parent.document.getElementById('terarium-code-cell');

  // Setup the keydown listener for the document.
  document.addEventListener(
    'keydown',
    event => {
      commands.processKeydownEvent(event);
    },
    useCapture
  );

  // Create the cell widget with a default rendermime instance.
  const rendermime = new RenderMimeRegistry({ initialFactories });

  // const cellWidget = new LLMCell({
  const cellWidget = new LLMCell({
    contentFactory: new Cell.ContentFactory({
      editorFactory: factoryService.newInlineEditor.bind(factoryService)
    }),
    rendermime,
    model: new LLMCellModel()
  }).initializeState();
  cellWidget.id = "code-cell";
  cellWidget.outputArea.id = "code-output";

  // Enable line numbers
  cellWidget.editorConfig.lineNumbers = true;

  // Handle the mimeType for the current kernel asynchronously.
  sessionContext.kernelChanged.connect(() => {
    // TODO: Add a spinner here, probably, as running the setup code can take a second (longer in Julia than Python)
    const kernel = sessionContext.session?.kernel
    void kernel?.info.then(async info => {
      const lang = info.language_info;
      const mimeType = mimeService.getMimeTypeByLanguage(lang);
      cellWidget.model.mimeType = mimeType;

      let setupCode = getCodeBlock(lang.name);
      console.log(setupCode);

      const future = kernel?.requestExecute({
        code: setupCode, 
        silent: true,
        store_history: false,
      }, false);
      if (future) {
        await future.done;
        // TODO: Remove spinner after completed
      }
    });
  });


  // Use the default kernel (Python).
  sessionContext.kernelPreference = { autoStartDefault: true };

  // Set up a completer.
  const editor = cellWidget.editor;
  const model = new CompleterModel();
  const completer = new Completer({ editor, model });
  const timeout = 1000;
  const provider = new KernelCompleterProvider();
  const reconciliator = new ProviderReconciliator({
    context: { widget: cellWidget, editor, session: sessionContext.session },
    providers: [provider],
    timeout: timeout
  });
  const handler = new CompletionHandler({ completer, reconciliator });

  void sessionContext.ready.then(() => {
    const provider = new KernelCompleterProvider();
    handler.reconciliator = new ProviderReconciliator({
      context: { widget: cellWidget, editor, session: sessionContext.session },
      providers: [provider],
      timeout: timeout
    });
  });

  // Set the handler's editor.
  handler.editor = editor;

  // Hide the widget when it first loads.
  completer.hide();
  completer.addClass('jp-Completer-Cell');

  // Create a toolbar for the cell.
  const toolbar = new Toolbar();
  toolbar.addItem('spacer', Toolbar.createSpacerItem());
  // Removed the interrupt button because we might not want it, but keeping it commented out in case we want to put it back.
  // toolbar.addItem('interrupt', AppToolbar.createInterruptButton(sessionContext));
  toolbar.addItem('restart', AppToolbar.createRestartButton(sessionContext));
  toolbar.addItem('name', AppToolbar.createKernelNameItem(sessionContext));
  toolbar.addItem('status', AppToolbar.createKernelStatusItem(sessionContext));

  // Lay out the widgets.
  const panel = new BoxPanel();
  panel.id = 'main';
  panel.direction = 'top-to-bottom';
  panel.spacing = 0;
  panel.addWidget(toolbar);
  panel.addWidget(cellWidget);
  BoxPanel.setStretch(toolbar, 0);
  BoxPanel.setStretch(cellWidget, 1);

  // Attach the panel to the DOM.
  Widget.attach(panel, document.body);
  Widget.attach(completer, document.body);

  // Handle widget state.
  window.addEventListener('resize', () => {
    panel.update();
  });
  cellWidget.activate();

  // Add the commands.
  commands.addCommand('invoke:completer', {
    execute: () => {
      handler.invoke();
    }
  });
  commands.addCommand('run:cell', {
    execute: async () => {
      LLMCell.execute(cellWidget, sessionContext).then(() => {
        // Trigger automatic actions for when the cell is ran
        const event = new Event("cell:ran");
        parentIframe?.dispatchEvent(event);
      }).catch((err) => {
        // Catches errors sending to the kernel, not errors in the code itself. 
        // Those errors are returned in the cell output formatted as html.
        console.warn("Error executing jupyter cell", err);
      });
    }
  });

  // Add key bindings
  commands.addKeyBinding({
    selector: '.jp-InputArea-editor.jp-mod-completer-enabled',
    keys: ['Tab'],
    command: 'invoke:completer'
  });
  commands.addKeyBinding({
    selector: '.jp-InputArea-editor',
    keys: ['Shift Enter'],
    command: 'run:cell'
  });
  commands.addKeyBinding({
    selector: '.jp-InputArea-editor',
    keys: ['Ctrl Enter'],
    command: 'run:cell'
  });
  commands.addKeyBinding({
    selector: '.jp-InputArea-editor',
    keys: ['Alt Enter'],
    command: 'run:cell'
  });

  // Linkage from a DOM event to a Jupyter command to allow runs to be triggered from parent windows/externally.
  window.addEventListener('run:cell', () => {
    commands.execute("run:cell");
  });

  // Start up the kernel.
  void sessionContext.initialize();

}

window.addEventListener('load', main);
