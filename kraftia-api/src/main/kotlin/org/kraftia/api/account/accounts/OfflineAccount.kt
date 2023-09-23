package org.kraftia.api.account.accounts

import org.kraftia.api.account.AbstractAccount

class OfflineAccount(name: String, uuid: String? = null) : AbstractAccount(uuid ?: uuid(name), name)