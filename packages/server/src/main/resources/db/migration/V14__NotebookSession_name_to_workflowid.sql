UPDATE notebook_session
	SET workflow_id=cast(name as uuid),
			name=NULL

